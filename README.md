The basic idea of this prototype is:
- Instrument a Spring REST application to expose Prometheus metrics, useful to monitor HTTP calls
- Deploy one or more instances of such application in a Kubernetes cluster (currently a Microk8s cluster in a VM with Ubuntu)
- Set up a Prometheus server (Docker image seems the easiest way) configured to do service discovering through Kubernetes of the Spring applications and to scape metrics from them  
- Deploy a stack based on Fluentbit and Loki to aggregate logs on Loki
- Configure Grafana dashboards and alerts for Prometheus and Loki data sources
  
# Setup microk8s in a Ubuntu VM
- follow steps in Microk8s docs https://microk8s.io/docs
  `sudo snap install microk8s --classic`
  `sudo usermod -a -G microk8s $USER`
  `sudo chown -f -R $USER ~/.kube`
  `su - $USER`
  
- enable Microk8s add ons (should be optional; check if some add one is required for the prototype)
  `sudo microk8s enable dns metrics-server dashboard ingress`

# Build the Docker image
- run Gradle bootJar: `.\gradlew.bat clean bootJar`
- build and tag the docker image: `docker build -t ntuveri/spring-monitoring .` where ntuveri is a valid account in Docker Hub
- Docker Hub login with `docker login` with proper username and password 
- push the local docker image to the remote repository: `docker push ntuveri/spring-monitoring`

# Apply kubernetes manifest files

# Run Prometheus with Docker in Windows Powershell 
`docker run -d --name prometheus -p 9090:9090 -v $pwd/prometheus:/etc/prometheus prom/prometheus`

# Run Loki with Docker
`docker run -d --name loki -p 3100:3100 -v $pwd/loki/loki-config.yaml:/etc/loki/local-config.yaml grafana/loki`

# Run Grafana with Docker 
`docker run -d --name grafana -p 3000:3000 -v $pwd/grafana/conf/custom.ini:/etc/grafana/grafana.ini -v $pwd/grafana/provisioning:/etc/grafana/provisioning grafana/grafana`
It should provision at startup data sources, dashboards, notifiers and SMTP configuration   
Set up the Prometheus data source and install the following dashboards:
- add the Prometheus datasource (can be accessed trough host network with http://host.docker.internal:9090)
- add the JVM micrometer dashboard at https://grafana.com/grafana/dashboards/4701

# Sample Urls
Url for scraping metrics of a pod exposed with a service through the Kubernetes API proxy   
https://<Kubernetes-Node>:16443/api/v1/namespaces/default/services/spring-prometheus-svc:8080/proxy/actuator

Url for scraping metrics of a pod through the Kubernetes API proxy   
https://<Kubernetes-Node>:16443/api/v1/namespaces/default/pods/spring-prometheus:8080/proxy/actuator

Default bearer token to access Kubernetes API server
`token=$(microk8s kubectl -n kube-system get secret | grep default-token | cut -d " " -f1)`
`microk8s kubectl -n kube-system describe secret $token`

# Tasks
1) Set label application in metrics as expected by the JVM Micrometer Grafana Dashboard - Ok  
2) Test multiple replicas of the same app - Ok
3) Test different app - Ok
4) Test logback metrics
5) Test memory kubernetes limits and JVM memory usage (try to replicate OutOfMemory exception) - Ok
6) Add Kubernetes metrics and dashboard - Ok
7) Alerts based on metrics - OK
8) Alerts based on logs
9) Set up an example of context, trace and span ids
10) Set up Jaeger / Zipkin and explore traces 
11) Set up cross-links between logs and traces 
