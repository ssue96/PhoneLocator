var express = require('express'); //express

//firebase
//https://console.firebase.google.com/
//->select your projcet -> project overview -> setting -> service account
//you can get this code
var admin = require('firebase-admin');
var serviceAccount = require('path/serverAccount.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "databaseURL-from-firebase-console"
});


//mysql connection start
var mysql = require('mysql');
var con = mysql.createConnection({
    host: 'host',
    user: 'user',
    password: 'password',
    port: '3306',
    database: 'my_db'
});

var client = mysql.createConnection({
    user: 'user',
    password: 'password'
});
client.connect();
client.query('use my_db');


con.connect(function (err) {
    console.log("Connected!");
});
//mysql connection finish

//npm fcm
var FCM = require('fcm-node');
var serverKey = 'server-key';
//on the firebase website
var client_token = 'client-token';
//if you build android application, you can get client token from the console

var app = express();
app.set('port', process.env.PORT || 3000); //set port
var bodyParser = require('body-parser'); //use body-parser
app.use(bodyParser.urlencoded({
    extended: true
})); //support encoded bodies
app.use(bodyParser.json()); //support json encoded bodies

var fs = require('fs'); //file load

//var querystring = require('querystring');

//app.use(express.static('phoneLocator'));

//main page
app.get('/', function (req, res) {
    res.sendFile(__dirname + "/html/main.html");
    console.log('main test');
});

//if you push the button on the website, you can see the marker on the google map
app.get('/getData', function (req, res) {
    var data;
//send data in HISTORY table to client through ajax
    client.query('SELECT * FROM history', function (err, rows, fields) {
        if (err) {
            throw err;
        } else {
            console.log('select test : DB Connect success\n');
            console.log(rows);
            console.log('\n');
            data = rows;
            return res.json(data);
        }
    });;
});

//main.js load
app.get('/js/main.js', function (req, res) {
    fs.readFile('js/main.js', function (err, data) {
        if (err) {
            console.log(err);
            res.writeHead(404, {
                'Content-Type': 'text/javascript'
            });
            res.end('<h1>404 page not found~</h1>');
        } else {
            res.writeHead(200, {
                'Content-Type': 'text/javascript'
            });
            res.end(data);
        }
    });
});

//if you push the button, arduino requests host/pl
//send data message to your phone
app.get('/pl', function (req, res) {
    console.log("button pushed");
    var push_data = {
        to: client_token,
        data: {
            "test": "1"
        },
        priority: "high",
        restricted_package_name: "package-name",
    };
    //set your package name
    
    var fcm = new FCM(serverKey);
    fcm.send(push_data, function (err, res) {
        if (err) {
            console.log('push failed');
            console.error(err);
            console.error('push failed_err');
            return;
        }
        console.log('push success');
        console.log(res);
    });
    res.send('Button pushed');
});


//post page 
//android send json data {email, date, latitude, longitude} to server
app.post('/post', function (req, res) {
    console.log("test post");
    var inputData = req.body;
    console.log(inputData);
    res.send(inputData);
    //insert data from application to HISTORY table 
    client.query('INSERT INTO history(user_id, email, date, latitude, longitude) VALUES (?, ?, ?, ?, ?)', [1, inputData.email, inputData.date, inputData.latitude, inputData.longitude], function (err, res) {
        if (err) {
            console.error(err);
        } else {}
        //print saved data in HISTORY table
        client.query('SELECT * FROM history', function (err, rows, fields) {
            if (err) {
                throw err;
            } else {
                console.log('select test : DB Connect success\n');
                console.log(rows);
                console.log('\n');
            }
        });
        client.end();
    });

});

app.listen(app.get('port'), function () {
    console.log('Express server listening on port ' + app.get('port'));
});
