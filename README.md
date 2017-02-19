# nginx + Java = nginj

FastCGI implemention in Java with at runtime .java file compilation.


## Dependencies
- openjdk-8-jdk

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