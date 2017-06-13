 Depoy to cf
 1. Create user provided services for config-server, eureka-server
  cf cups config-server -p '{"uri":"http://config-server-ss.local.pcfdev.io"}
  cf cups eureka-server -p '{"uri":"http://eureka-server-ss.local.pcfdev.io"}

 2. Change each microservice's bootstrap.properties. Modify config-server uri to use cf backend service.

 3. Build all projects
   ./mvnw package -Dmaven.test.skip=true

 4. Create manifest.yml for all microservices for all microservices.
      config-server have not to bind any services.
      eureka-server must bind config-server service instance.
      other-service must bind config-server and eureka-server service instances.

 5. Push all app to cloudfoundry.
      cf push config-server-ss
      cf push eureka-server-ss
      cf push membership-ss
      cf push recommendations-ss
      cf push ui-ss

 6. Access
     access to http://ui-ss.local.pcfdev.io
     
     access to http://eureka-server-ss.local.pcfdev.io
      if you scale apps, you can see eureka dashboard chach there instances.
      if you stop app, apps run correctlly becaouse of circuit breaker.

