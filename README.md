# TPE POD Grupo 2

## Requisitos
* Java 21
* Maven

## Compilado
Ejecutar para compilar el proyecto
```bash
$ ./tpe_builder.sh -s -c
```

## Ejecucción
### Servidor
Para correr el servidor, ejecutar:
```bash
$ ./bin/server/hospitalServer.sh
```

### Cliente
Para correr el Servicio de Administración, ejecutar:
```bash
$ ./bin/client/administrationClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
-Daction=actionName [ -Ddoctor=doctorName | -Dlevel=levelNumber |
-Davailability=availabilityName ]
```

Para correr el Servicio de Sala de Espera, ejecutar:
```bash
$ ./bin/client/waitingRoomClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
-Daction=actionName [ -Dpatient=patientName | -Dlevel=levelNumber ]
```

Para correr el Servicio de Atención de Emergencias, ejecutar:
```bash
$ ./bin/client/emergencyCareClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
-Daction=actionName [ -Droom=roomNumber | -Ddoctor=doctorName |
-Dpatient=patientName ]
```

Para correr el Servicio de Notificación al Personal, ejecutar:
```bash
$ ./bin/client/doctorPagerClient.sh -DserverAddress=xx.xx.xx.xx:yyyy
-Daction=actionName -Ddoctor=doctorName
```

Para correr el Servicio de Consulta, ejecutar:
```bash
$ ./bin/client/queryClient.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName
-DoutPath=filePath.csv [ -Droom=roomNumber ]
```
