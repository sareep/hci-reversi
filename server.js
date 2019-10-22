/*include static file webserver library*/
var static = require('node-static');

/*Include the http server library*/
var http = require('http');

/* Assume running on Heroku */
var port = process.env.PORT;
var directory = __dirname + '/public';

/*If not on Heroku, adjust port and directory info*/
if(typeof(port) == 'undefined' || !port){
    directory = './public';
    port = 8080;
}

/*Set up static web server. Delivers files from filesystem*/
var file = new static.Server(directory);

/*Construct HTTP server that gets files from fileserver*/
var app = http.createServer(
    function(request,response){
        request.addListener('end', 
            function(){
                file.serve(request,response);
            }
        ).resume();
    }
).listen(port);

console.log('The Server is running');