/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is OpenELIS code.
 */
package us.mn.state.health.lims.analyzerimport.action;

import org.json.JSONObject;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.AbxMicrosReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.Advia560Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.Amha1806Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.BC3000Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.BC30Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.BKReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.BS200EReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.BS230Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.CLReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.COBAS6000Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.CobasC311JsonReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.Cobase402Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.DF5Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.DxH800SingleParametreReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.Dxc700Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.DxhAllParametreReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.Dxi800Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.DymindDH36;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.Exl200Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.FNReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.GeneXpertReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.Humastar200;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.MN5150Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.MindrayBC780Reader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.MindrayCL980IReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.SysmexKXReader;
import us.mn.state.health.lims.analyzerimport.analyzerreaders.SysmexXNReader;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Routes JSON analyzer payloads by machineName.
 * More specific tokens are matched before short/ambiguous ones (e.g. cl_980i before cl).
 */
final class AnalyzerJsonRouter {

    private interface Matcher {
        boolean matches(String machineName);
    }

    private static final class Route {
        private final Matcher matcher;
        private final AnalyzerJsonResultHandler handler;

        private Route(Matcher matcher, AnalyzerJsonResultHandler handler) {
            this.matcher = matcher;
            this.handler = handler;
        }
    }

    private final List<Route> routes = new ArrayList<Route>();

    private AnalyzerJsonRouter() {
    }

    static AnalyzerJsonRouter forAddAnalyzerData() {
        AnalyzerJsonRouter router = new AnalyzerJsonRouter();
        // Specific-first order preserves Wachemo machineName tokens while avoiding short-token collisions.
        router.addContainsIgnoreCase("dxh 800", wrap(new DxH800SingleParametreReader()));
        router.addContainsIgnoreCase("exl 200", wrap(new Exl200Reader()));
        router.addContainsIgnoreCase("abx es 60", wrap(new AbxMicrosReader()));
        router.addContainsIgnoreCase("dxi 800", wrap(new Dxi800Reader()));
        router.addContains("SysmexXN550", wrap(new SysmexXNReader())); // Wachemo uses case-sensitive match
        router.addContainsIgnoreCase("bc3000", wrap(new BC3000Reader()));
        router.addContainsIgnoreCase("cobas c311", wrap(new CobasC311JsonReader()));
        router.addContainsIgnoreCase("dxc 700", wrap(new Dxc700Reader()));
        router.addContainsIgnoreCase("genexpert", wrap(new GeneXpertReader()));
        router.addContainsIgnoreCase("sysmexkx", wrap(new SysmexKXReader()));
        router.addContainsIgnoreCase("humastar", wrap(new Humastar200()));
        router.addContainsIgnoreCase("bs200e", wrap(new BS200EReader()));
        router.addContainsIgnoreCase("dymind_dh36", wrap(new DymindDH36()));
        router.addContainsIgnoreCase("mn5150", wrap(new MN5150Reader()));
        router.addContainsIgnoreCase("bc_780", wrap(new MindrayBC780Reader()));
        router.addContainsIgnoreCase("cl_980i", wrap(new MindrayCL980IReader()));
        router.addContainsIgnoreCase("bc30", wrap(new BC30Reader()));
        router.addContainsIgnoreCase("df5", wrap(new DF5Reader()));
        router.addContainsIgnoreCase("bs230", wrap(new BS230Reader()));
        router.addContainsIgnoreCase("cobas6000", wrap(new COBAS6000Reader()));
        router.addContainsIgnoreCase("e402", wrap(new Cobase402Reader()));
        router.addContainsIgnoreCase("am1806", wrap(new Amha1806Reader()));
        router.addContainsIgnoreCase("adv", wrap(new Advia560Reader()));
        router.addContainsIgnoreCase("bk", wrap(new BKReader()));
        router.addContainsIgnoreCase("fn", wrap(new FNReader()));
        router.addContainsIgnoreCase("cl", wrap(new CLReader()));
        return router;
    }

    static AnalyzerJsonRouter forInsertAnalyzerData() {
        AnalyzerJsonRouter router = new AnalyzerJsonRouter();
        router.addContainsIgnoreCase("dxh 800", wrap(new DxhAllParametreReader()));
        return router;
    }

    private static AnalyzerJsonResultHandler wrap(final Object reader) {
        return new AnalyzerJsonResultHandler() {
            public boolean insertResult(JSONObject json) throws Exception {
                try {
                    Object result = reader.getClass()
                            .getMethod("insertResult", JSONObject.class)
                            .invoke(reader, json);
                    return result instanceof Boolean ? ((Boolean) result).booleanValue() : true;
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof Exception) {
                        throw (Exception) cause;
                    }
                    throw e;
                }
            }
        };
    }

    private void addContainsIgnoreCase(final String token, AnalyzerJsonResultHandler handler) {
        routes.add(new Route(new Matcher() {
            public boolean matches(String machineName) {
                return machineName != null && machineName.toLowerCase().contains(token);
            }
        }, handler));
    }

    private void addContains(final String token, AnalyzerJsonResultHandler handler) {
        routes.add(new Route(new Matcher() {
            public boolean matches(String machineName) {
                return machineName != null && machineName.contains(token);
            }
        }, handler));
    }

    AnalyzerJsonResultHandler findHandler(String machineName) {
        for (Route route : routes) {
            if (route.matcher.matches(machineName)) {
                return route.handler;
            }
        }
        return null;
    }
}
