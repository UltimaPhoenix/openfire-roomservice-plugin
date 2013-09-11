openfire-roomservice-plugin
===========================

Openfire plugin to allows administration of rooms via HTTP requests.

Configuration page included into plugin like userservice plugin.
The service address is [hostname]/plugins/roomservice/roomservice
Parameters:
* jid
* roomName
* secret
* subdomain
* type

Supported type: 'add' or 'delete'.

Reference http://www.igniterealtime.org/projects/openfire/plugins/userservice/readme.html

Remember to configure plugin in Openfire admin console.
