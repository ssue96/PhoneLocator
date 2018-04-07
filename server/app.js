
var express = require('express');
var admin = require('firebase-admin');
var serviceAccount = require('/path/serverAccount.json');
admin.initializeApp({
        credential : admin.credential.cert(serviceAccount),
        databaseURL : "databaseURL-from-firebase-console"
});
//npm fcm
var FCM = require('fcm-node');
var serverKey = 'server-Key';
//on the firebase website
var client_token = 'Client-key';
//if you run android application, you can get client token from the console
var app = express();
app.set('port', process.env.PORT || 3000);
//main page
app.get('/', function(req, res){
        res.send('main');
});
//test page
app.get('/test', function(req,res){
                if(err){
                        console.log('err');
                        throw err;
                }
                else{
                        console.log("success /n ");
                }
});
//if you push the button, arduino requests host/pl
app.get('/pl', function(req, res){
        console.log("button pushed");
        var push_data = {
                to: client_token,
                data: {
                        "test" : "1"
                },
                priority: "high",
                restricted_package_name: "package-name",
        };
        var fcm = new FCM(serverKey);
        fcm.send(push_data, function(err, res){
                if(err){
                        console.log('push failed');
                        console.err(err);
                        console.err('push filed_err');
                        return;
                }
                console.log('push success');
                console.log(res);
        });
        res.send('Button pushed');
});
app.listen(app.get('port'), function(){
        console.log('Express server listening on port '+ app.get('port'));
});
