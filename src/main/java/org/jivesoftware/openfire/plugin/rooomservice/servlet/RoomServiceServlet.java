/**
 * $RCSfile$
 * $Revision: 1710 $
 * $Date: 2005-07-26 11:56:14 -0700 (Tue, 26 Jul 2005) $
 *
 * Copyright (C) 2004-2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.plugin.rooomservice.servlet;

import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.NotAllowedException;
import org.jivesoftware.openfire.plugin.rooomservice.RoomServicePlugin;
import org.jivesoftware.util.Log;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet that addition/deletion/modification of the users info in the system.
 * Use the <b>type</b>
 * parameter to specify the type of action. Possible values are <b>add</b>,<b>delete</b> and
 * <b>update</b>. <p>
 * <p/>
 * The request <b>MUST</b> include the <b>secret</b> parameter. This parameter will be used
 * to authenticate the request. If this parameter is missing from the request then
 * an error will be logged and no action will occur.
 *
 * @author Justin Hunt
 */
public class RoomServiceServlet extends HttpServlet {

    private static final long serialVersionUID = -7039598193937438431L;
    
    private RoomServicePlugin plugin;

    @Override
	public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        plugin = (RoomServicePlugin) XMPPServer.getInstance().getPluginManager().getPlugin("roomservice");
 
        // Exclude this servlet from requiring the user to login
        AuthCheckFilter.addExclude("roomservice/roomservice");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Printwriter for writing out responses to browser
        PrintWriter out = response.getWriter();

        if (!plugin.getAllowedIPs().isEmpty()) {
            // Get client's IP address
            String ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null) {
                ipAddress = request.getHeader("X_FORWARDED_FOR");
                if (ipAddress == null) {
                    ipAddress = request.getHeader("X-Forward-For");
                    if (ipAddress == null) {
                        ipAddress = request.getRemoteAddr();
                    }
                }
            }
            if (!plugin.getAllowedIPs().contains(ipAddress)) {
                Log.warn("User service rejected service to IP address: " + ipAddress);
                replyError("RequestNotAuthorised",response, out);
                return;
            }
        }

//        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//        String name = request.getParameter("name");
//        String email = request.getParameter("email");
        String type = request.getParameter("type");
        String secret = request.getParameter("secret");
//        String groupNames = request.getParameter("groups");
        
        String jid = request.getParameter("jid");
        String subdomain = request.getParameter("subdomain");
        String roomName = request.getParameter("roomName");
        
        //No defaults, add, delete, update only
        //type = type == null ? "image" : type;
       
       // Check that our plugin is enabled.
        if (!plugin.isEnabled()) {
            Log.warn("Room service plugin is disabled: " + request.getQueryString());
            replyError("RoomServiceDisabled",response, out);
            return;
        }
       
        // Check this request is authorised
        if (secret == null || !secret.equals(plugin.getSecret())){
            Log.warn("An unauthorised user service request was received: " + request.getQueryString());
            replyError("RequestNotAuthorised",response, out);
            return;
         }

        // Some checking is required on the username
//        if (username == null){
//            replyError("IllegalArgumentException",response, out);
//            return;
//        }


        // Check the request type and process accordingly
        try {
//            username = username.trim().toLowerCase();
//            username = JID.escapeNode(username);
//            username = Stringprep.nodeprep(username);
            if ("add".equals(type)) {
                plugin.createChat(jid, subdomain, roomName);
                replyMessage("ok",response, out);
                //imageProvider.sendInfo(request, response, presence);
            }
            else if ("delete".equals(type)) {
                plugin.deleteChat(jid, subdomain, roomName);
                replyMessage("ok",response,out);
                //xmlProvider.sendInfo(request, response, presence);
            } else {
                Log.warn("The userService servlet received an invalid request of type: " + type);
                // TODO Do something
            }
        } catch (NotAllowedException e) {
            replyError("NotAllowedException",response, out);
        } catch (IllegalArgumentException e) {
            replyError("IllegalArgumentException",response, out);
//        } catch (StringprepException e) {
//            replyError("StringprepException " + e.getMessage(), response, out);
        } catch (RuntimeException e) {
            replyError(e.toString(),response, out);
        }
    }

    private void replyMessage(String message,HttpServletResponse response, PrintWriter out){
        response.setContentType("text/xml");        
        out.println("<result>" + message + "</result>");
        out.flush();
    }

    private void replyError(String error,HttpServletResponse response, PrintWriter out){
        response.setContentType("text/xml");        
        out.println("<error>" + error + "</error>");
        out.flush();
    }
    
    @Override
	public void destroy() {
        super.destroy();
        // Release the excluded URL
        AuthCheckFilter.removeExclude("roomService/roomservice");
    }
}
