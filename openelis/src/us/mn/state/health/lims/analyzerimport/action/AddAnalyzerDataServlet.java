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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import us.mn.state.health.lims.login.dao.LoginDAO;
import us.mn.state.health.lims.login.daoimpl.LoginDAOImpl;
import us.mn.state.health.lims.login.valueholder.Login;
import us.mn.state.health.lims.systemuser.dao.SystemUserDAO;
import us.mn.state.health.lims.systemuser.daoimpl.SystemUserDAOImpl;
import us.mn.state.health.lims.systemuser.valueholder.SystemUser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Accepts JSON analyzer results from instrument middleware (/addAnalyzerData).
 * Matching tokens and reader behavior match Wachemo production; routing is specific-first.
 */
public class AddAnalyzerDataServlet extends HttpServlet {

    private static final AnalyzerJsonRouter ROUTER = AnalyzerJsonRouter.forAddAnalyzerData();

    private String systemUserId;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BufferedReader bufferedReader = request.getReader();
        JSONTokener tokener = new JSONTokener(bufferedReader);

        // Auth intentionally matches current Wachemo deploy (optional Basic auth not enforced).
        // Keep the helper below for sites that choose to re-enable it.

        try {
            JSONObject json = new JSONObject(tokener);
            String analyzerName = json.getString("machineName");
            AnalyzerJsonResultHandler handler = ROUTER.findHandler(analyzerName);
            if (handler == null) {
                response.getWriter().print("Unknown analyzer with name: " + analyzerName);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            handler.insertResult(json);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (JSONException e) {
            response.getWriter().print("Invalid analyzer JSON payload");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            throw new ServletException("Failed to import analyzer data", e);
        }
    }

    @SuppressWarnings("unused")
    private boolean userValid(String user, String password) {
        Login login = new Login();
        login.setLoginName(user);
        login.setPassword(password);

        LoginDAO loginDAO = new LoginDAOImpl();
        login = loginDAO.getValidateLogin(login);

        if (login == null) {
            return false;
        }
        SystemUserDAO systemUserDAO = new SystemUserDAOImpl();
        SystemUser systemUser = systemUserDAO.getDataForLoginUser(login.getLoginName());
        systemUserId = systemUser.getId();
        return true;
    }
}
