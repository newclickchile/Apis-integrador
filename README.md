# Aplicación Integrador - Newclick

## Descripción
Este proyecto es una aplicación web desarrollada en Java 21 que se ejecuta en un servidor Apache Tomcat 11. La aplicación ofrece funcionalidades de ejemplo y demuestra cómo configurar y desplegar una aplicación Java en Tomcat.

## Requisitos
- JDK 21
- Apache Tomcat 11
- Maven 3.x (opcional, para gestionar dependencias y construir el proyecto)

## Instalación

### Clonar el repositorio
Este proyecto .....

```sh
git clone https://github.com/tu-usuario/tu-repositorio.git
cd tu-repositorio
```

### Construir la aplicación
Este proyecto .....
```sh
./mvnw verify
```

### Subir archivo WAR
Este proyecto .....

### Desplegar en Tomcat
Este proyecto .....
```sh
systemctl stop tomcat
mv /home/ubuntu/*.war /opt/apache-tomcat-11.0.2/webapps-javaee/
systemctl start tomcat
```

### Archivos de LOG
Este proyecto .....
```sh
tail -f  /var/log/integrador/int-bsale-woowup.log
tail -f  /var/log/integrador/conf-bsale-woowup.log
```
