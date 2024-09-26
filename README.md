<h1 align="center">Sala de emergencias</h1>
<h3 align="center">Trabajo Práctico Especial 1 - Grupo 2</h3>

# Requisitos

* Java 21
* Maven

# Compilado

## A mano

Para preparar el entorno de ejecución a partir del código fuente del proyecto se
deben ejecutar los siguientes comandos por consola:

```bash
$ mvn clean package
````

> Se puede especificar `-am <module> -pl` para armar el
> package de un modulo en particular

Una vez hecho esto, el modulo `api` se habrá compilado y los módulos `server` y
`client` habrán generado comprimidos con extensión `.tar.gz` en las carpetas
`target/` de cada módulo.

Para descomprimir dichos archivos, donde se encuentran los archivos de código
fuente compilados y los *shell scripts* para ejecutar el proyecto hay que
ejecutar los siguientes comandos (desde el *root* del proyecto):

```bash
# Para el modulo `server`
cd server/target
tar xzf tpe1-g2-server-1.0-SNAPSHOT-bin.tar.gz

# Para el modulo `client` (nuevamente desde el root)
cd client/server
tar xzf tpe1-g2-client-1.0-SNAPSHOT-bin.tar.gz
```

Una vez descomprimidos los archivos, dentro de cada carpeta generada se
encontrarán los *shell scripts* que permitirán poner en funcionamiento el
proyecto. Para esto hay que marcarlos como **ejecutables**:

```bash
# Para el modulo `server`
cd server/target/tpe1-g2-server-1.0-SNAPSHOT
chmod +x *.sh

# Para el modulo `client`
cd server/target/tpe1-g2-client-1.0-SNAPSHOT
chmod +x *.sh
```

Una vez hecho esto, dentro de cada carpeta (en instancias terminales diferentes)
se puede pasar a la etapa de [ejecución](#ejecución).

## Script especial

Sin embargo, como hacer esto a mano es bastante molesto, desarrollamos un
*script* que permite automatizar y facilitar esta tarea y dejar los archivos
ubicados en una posición más cómoda. Dicho *script* se llama `tpe_builder.sh` y
al ejecutarlo se puede especificar los siguientes flags:

- **`-s` o `--server`:** Solo compila, desempaqueta y reubica el módulo de server.
- **`-c` o `--client`:** Solo compila, desempaqueta y reubica el módulo de cliente.
- **`-C` o `--clean`:** Al hacer las tareas, también ejecuta `mvn clean` para compilar desde cero.
- **`-h` o `--help`:** Imprime el mensaje de ayuda.

Por lo tanto, para obtener el resultado de los pasos descriptos en
[la compilación a mano](#a-mano) hay que ejecutar lo siguiente:

```bash
$ ./tpe_builder.sh -s -c -C
```

Los archivos resultantes se encontrarán bajo `bin/client` y `bin/server` para
los módulos de cliente y servidor respectivamente permitiendo que los *scripts*
del proyecto se puedan ejecutar más fácilmente haciendo:

```bash
$ ./bin/server/hospitalServer.sh
```

# Ejecución

## Servidor

Para correr el servidor, ejecutar:

```bash
$ ./bin/server/hospitalServer.sh
```

## Cliente

### Servicio de Administración

Para correr el Servicio de Administración, ejecutar:

```bash
$ ./bin/client/administrationClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
-Daction=actionName [ -Ddoctor=doctorName | -Dlevel=levelNumber |
-Davailability=availabilityName ]
```

Los flags posibles son:

- **Para agregar un consultorio:** `-Daction=addRoom`
- **Para agregar un médico:** `-Daction=addDoctor`, `-Ddoctor=<nombre_doctor>` y `-Dlevel=<numero_nivel>`
- **Para definir la disponibilidad de un médico:** `-Daction=setDoctor`, `-Ddoctor=<nombre_doctor>` y `-Davailability=<disponibilidad>`
- **Para consultar la disponibilidad de un médico:** `-Daction=checkDoctor` y `-Ddoctor=<nombre_doctor>`

### Servicio de Sala de Espera

Para correr el Servicio de Sala de Espera, ejecutar:

```bash
$ ./bin/client/waitingRoomClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
-Daction=actionName [ -Dpatient=patientName | -Dlevel=levelNumber ]
```

Los flags posibles son:

- **Para registrar un paciente:** `-Daction=addPatient`, `-Dpatient=<nombre_paciente>` y `-Dlevel=<nivel_emergencia>`
- **Para actualizar el nivel de emergencia de un paciente:** `-Daction=updateLevel`, `-Dpatient=<nombre_paciente>` y `-Dlevel=<nivel_emergencia>`
- **Para consultar la espera aproximada de un paciente:** `-Daction=checkPatient` y `-Dpatient=<nombre_paciente>`

### Servicio de Atención de Emergencias

Para correr el Servicio de Atención de Emergencias, ejecutar:

```bash
$ ./bin/client/emergencyCareClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
-Daction=actionName [ -Droom=roomNumber | -Ddoctor=doctorName |
-Dpatient=patientName ]
```

Los flags posibles son:

- **Para iniciar la atención de una emergencia en un consultorio:** `-Daction=carePatient` y `-Droom=<numero_consultorio>`
- **Para iniciar la atención de emergencias en los consultorios libres:** `-Daction=careAllPatients`
- **Para finalizar la atención de una emergencia en un consultorio:** `-Daction=dischargePatient`, `-Droom=<numero_consultorio>`, `-Ddoctor=<nombre_doctor>` y `-Dpatient=<nombre_paciente>`

### Servicio de Notificación al Personal

Para correr el Servicio de Notificación al Personal, ejecutar:

```bash
$ ./bin/client/doctorPagerClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
-Daction=actionName -Ddoctor=doctorName
```

Los flags posibles son:

- **Para registrar a un médico para ser notificado:** `-Daction=register` y `-Ddoctor=<nombre_doctor>`
- **Para anular el registro de un médico:** `-Daction=unregister` y `-Ddoctor=<nombre_doctor>`

### Servicio de Consulta

Para correr el Servicio de Consulta, ejecutar:

```bash
$ ./bin/client/queryClient.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName
-DoutPath=filePath.csv [ -Droom=roomNumber ]
```

Los flags posibles son:

- **Para consultar el estado actual de los consultorios:** `-Daction=queryRooms` y `-DoutPath=<ubicacion_archivo>`
- **Para consultar los pacientes esperando a ser atendidos:** `-Daction=queryWaitingRoom` y `-DoutPath=<ubicacion_archivo>`
- **Para consultar las atenciones finalizadas:** `-Daction=queryCares` y `-DoutPath=<ubicacion_archivo>`
