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

/**
 * Handles a JSON analyzer payload for a specific instrument.
 */
public interface AnalyzerJsonResultHandler {
    boolean insertResult(JSONObject json) throws Exception;
}
