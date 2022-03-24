# Quarkus app for producing Order events to Kafka using reactive messaging

This code is to present a Microprofile reactive messaging producer with Schema registry integration.
This is an instantiation of EDA Quickstart template named: [quarkus-reactive-kafka-producer](https://github.com/ibm-cloud-architecture/eda-quickstarts/tree/main/quarkus-reactive-kafka-producer).

## How this app was created

We use the new Quarkus CLI to create the basic project:

```sh
# Get the help
quarkus create app --help
# create a loan-origination bff app
quarkus create app  -x smallrye-openapi,smallrye-health,resteasy-mutiny,registry-avro,metrics,reactive-messaging-kafka ibm.eda.demo:eda-demo-order-ms:1.0.0
# Verify the app works
cd eda-demo-order-ms
quarkus dev
curl localhost:8080
```

Push to a github repository that you need to create in github. 

```sh
git init
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/jbcodeforce/eda-demo-order-ms.git
git push -u origin main
```

The code is coming from the eda-quickstart templates repository folder: `quarkus-reactive-kafka-producer`.

## Running the application in dev mode

* **Dev mode**: You can run your application in quarkus dev mode (which starts RedPanda Kafka and Apicurio 2.x in containers) to enable live coding using:

```shell script
quarkus dev
```

### Demonstration steps

* Access the application swagger-ui http://localhost:8080/q/swagger-ui

Go a GET on `/api/v1/orders` or using

```sh
curl -X 'GET' 'http://localhost:8080/api/v1/orders' -H 'accept: application/json'
```

* Using the Swagger-ui do a POST on `/api/v1/orders` or

```sh
curl -X 'POST' 'http://localhost:8080/api/v1/orders' -H 'accept: application/json' -H 'Content-Type: application/json' -d @./src/test/data/order_1.json
```

* When running in dev mode get the container id or name for redpanda and rexec into it, something like:

```sh
docker exec -ti f0db7829b31f bash
```

* Then use rpk CLI to get the records sent as a CloudEvent.

```sh
rpk topic consume eda-demo-orders 
```

Output example:

```json
 "headers": [
  {
   "key": "ce_specversion",
   "value": "1.0"
  },
  {
   "key": "ce_id",
   "value": "b77c955f-cfab-441a-8472-f2c4063a5002"
  },
  {
   "key": "ce_type",
   "value": "OrderEvent"
  },
  {
   "key": "ce_source",
   "value": "https://github.com/jbcodeforce/eda-demo-order-ms"
  },
  {
   "key": "ce_subject",
   "value": "OrderManager"
  },
  {
   "key": "ce_time",
   "value": "2022-01-07T17:09:24.694153America/Los_Angeles"
  },
  {
   "key": "apicurio.value.globalId",
   "value": "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001"
  },
  {
   "key": "apicurio.value.encoding",
   "value": "BINARY"
  }
 ],
 "key": "string",
 "message": "\u000cstring\u000cstring\u000cstring\u0014\u000epending\u000cstring\u000cstring\u000cstring\u000cstring\u000cstring\u000cstring\u000cstring\"OrderCreatedEvent",
 "partition": 0,
 "offset": 0,
 "timestamp": "2022-01-08T01:09:24.898Z"
}
```

* Get access to Apicurio UI to verify schema definition is uploaded.

```sh
# look at the port number apicurio is listening too
docker ps
# Use web browser on something like
http://localhost:64109/ui/artifacts/
# or curl
curl -X 'GET' 'http://localhost:64109/apis/registry/v2/search/artifacts?name=eda-demo-orders-value&offset=0&limit=20' \
  -H 'accept: application/json'
```

You should get a response like

```
{"artifacts":[{"id":"eda-demo-orders-value","name":"OrderEvent","createdOn":"2022-01-12T00:57:11+0000","createdBy":"","type":"AVRO","state":"ENABLED","modifiedOn":"2022-01-12T00:57:11+0000","modifiedBy":""}],"count":1}%  
```

* Start the simulation

```sh
./e2e/startSimulation.sh localhost:8080 20
```


## Running the app with Event Streams, Apicurio, Kafdrop

* You may be also able to start Kafka with docker compose using the compose file provided
in this folder: 

```sh
# under local-demo folder
docker compose up -d
```

you should see four containers running:

```sh
 ⠿ Container zookeeper      Started                                                                                                                     
 ⠿ Container kafka          Started     
 ⠿ Container apicurio       Started
 ⠿ Container kafdrop        Started  

```
* Start `quarkus dev`

* Go to the swagger UI: [http://localhost:8080/q/swagger-ui/](http://localhost:8080/q/swagger-ui/) or use
the following calls to get the connection to Kafka started:

```sh
curl -X 'GET' 'http://localhost:8080/api/v1/orders' -H 'accept: application/json'
```

* Go to Kafdrop console [http://localhost:9000/](http://localhost:9000/) to see messages in `orders` topic.

* Go to apicurio registry UI:  [http://localhost:8081/ui](http://localhost:8081/ui) to see the 
schema uploaded

* Start the simulation

```sh
./e2e/startSimulation.sh localhost:8080 20
```

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
# or with docker build and docker push
./script/buildAll.sh push
```

### Build and deploy on OpenShift using source to image

We assume you have Event Streams or Strimzi cluster deployed in one project.

* Create a OpenShift project: `oc new-project orderdemo`
* Copy secrets for ca-certificates and tls-user

  ```sh
    # use namespace where kafka runs
    NSSRC=eventstreams NSTGT=orderdemo SECRET=tlsuser \
  	oc get secret $SECRET --namespace=$NSSRC -o json \
	| jq  'del(.metadata.uid, .metadata.selfLink, .metadata.creationTimestamp, .metadata.ownerReferences)' \
	| jq -r '.metadata.namespace="'${NSTGT}'"' \
	 | oc apply --namespace=$NSTGT -f -
   # do the same with secret for the Kafka cluster ca cert. SECRET=dev-cluster-ca-cert  
  ```

* Build and deploy

```sh
mvn clean package -Dquarkus.container-image.build=true -Dquarkus.kubernetes.deploy=true -DskipTests
```
* Get the application route: `oc get route da-qs-order-ms`
* Send one order via the POST orders end point:

```sh
 {  "customerID": "C01",
    "productID": "P02",
    "quantity": 15,
    "destinationAddress": {
      "street": "12 main street",
      "city": "san francisco",
      "country": "USA",
      "state": "CA",
      "zipcode": "92000"
    }
}
```

* Verify messages are published to the topic, by getting to the Event Streams console.

### Deploy using public image and yamls

In the `kustomize` folder we have defined configmap, deployment,... that you can reuse to
deploy your app to OpenShift. 

* Update the `deployment.yaml` to reflect the secret names you are using for TLS user and ca cert.
* Update the configMap for the Apicurio URL to use external route.
* Doing an `oc apply -k kustomize` will deploy the current
`quay.io/ibmcase/eda-qs-order-ms` image to an OpenShift project. 

The following elements are created:

```
serviceaccount/qs-prod-sa created
rolebinding.rbac.authorization.k8s.io/app-sa-view created
configmap/qs-order-mgr-cm created
service/eda-qs-order-ms created
deployment.apps/qs-order-ms created
kafkatopic.eventstreams.ibm.com/qs-orders created
route.route.openshift.io/qs-order-ms created
```

## Integrating with GitOps

If you use OpenShift GitOps to deploy your solution, you can create your GitOps project with the kam CLI 
and then create a folder in the `environment/dev/apps` with the name
of your app based on this code, then copy the `kustomize` folder content under this newly
created folder. After that you need to add an Argocd app under the `config` folder.

See [this gitops](https://github.com/jbcodeforce/eda-demo-order-gitops) project to get more details on this deployment mode.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

