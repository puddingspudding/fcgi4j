# Nginx + Java = Nginj

FastCGI implemention in Java with at runtime .java file compilation.

## What, Why and How?
Nginx is one the most used web servers and as a Java developer you are not able to use it. If you want to provide a web service in Java you have to use 3rd party libraries like Jetty, Tomcat, Vert.x or Akka. This project's goal is to use Nginx and Java as easy as Nginx and PHP (PHP-FPM).
Nginj uses FastCGI to communicate with nginx on the same or dedicated server. Nginj compiles Java file on first sight and on any file changes in the configured root directory.


## Dependencies
- openjdk-8-jdk

## Install
```
sudo dpkg -i nginj.deb
```

## Configuration
### NGINX
```
location ~ \.java$ {
	include /etc/nginx/fastcgi_params;
	fastcgi_pass   127.0.0.1:9000;
}
```
---

### NGINJ
/etc/nginj/config.properties
```
#
# rootDirectory: directory to look for .java files
#
rootDirectory=/var/www

#
# tmpDir: temporary directory for .class files
#
tmpDirectory=/tmp/nginj

#
# host: address to listen
#
host=127.0.0.1

#
# port: port to listen
#
port=9000

#
# treads: number of threads accepting and handing requests
#
threads=10
```

## Build .deb
```
mvn clean package \
&& mv target/nginj-jar-with-dependencies.jar src/main/deb/usr/lib/nginj/nginj.jar \
&& cd src/main/deb \
&& tar -czf data.tar.gz  usr/ var/ etc/ \
&& tar -czf control.tar.gz prerm postinst postrm control conffiles \
&& ar r nginj.deb debian-binary control.tar.gz data.tar.gz \
&& rm data.tar.gz control.tar.gz \
&& cd -
```