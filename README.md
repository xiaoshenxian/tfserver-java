# tfserver-java

A TensorFlow server implemented in Java.

This project uses the Tomcat server framework and the [tfserver-core](https://github.com/xiaoshenxian/tfserver-core) jar to provide a Java version TensorFlow server for both HTTP Json and GRPC request interfaces.

## Usage

The [tfserver-core](https://github.com/xiaoshenxian/tfserver-core) jar is required.

```
<dependency>
    <groupId>com.eroelf</groupId>
    <artifactId>tfserver-core</artifactId>
    <version>${version}</version>
</dependency>
```

### Requester from clients

Generally, a request contains two part of data, the first one called *Sample* and the second one called *Param*. A *Sample* contains all the information needed to infer a TensorFlow model. A *Param* contains information that other than those required by TensorFlow models, so that it is optional, and in this case, it contains only one field called "trace" which is used for request tracing.

A *Sample* instance contains:
1. Model name.
2. Model version (optional, if not provided, the server will choose the newest version based on the model exported time).
3. Signature.
4. Inputs. This is basically a map contains inputs' name as key, and tensor data as values. Data transfer mechanism has been provided by *ArrayWrapper*s, users need only to provide the inputs' shapes and the real data, the server will converts them automatically. Data types can also be provided to *ArrayWrapper*s, but the server uses data types provided from those exported models, so the user provided ones will only be used for logging.

For each request, the server will return a data structure contains two parts of information, the *Status* and the *Data*. The *Status* gives the return status with code=0 for normal return, other code and the related information can be found in the [tfserver-core](https://github.com/xiaoshenxian/tfserver-core) jar. The *Data* contains all outputs of the model, which has the same data structure as the *Sample*'s inputs.

Please refer to the [exported TensorFlow models](https://www.tensorflow.org/serving/serving_basic#train_and_export_tensorflow_model) for the definitions of the model name, model version, signature, inputs' name, and outputs' name.

There are two kinds of clients this server supports, one is GRPC clients, the other is HTTP Json clients. **It is highly recommended to use the GRPC other than HTTP Json in production environments.**

#### GRPC client

Supports both "blocking unary call" and "asynchronous stream call". The [tfserver-core](https://github.com/xiaoshenxian/tfserver-core) provides tools for a convenient GRPC ProtoBuf access. Please refer to the [GrpcClientTest](./src/test/java/com/eroelf/tfserver/GrpcClientTest.java) file for both examples.

Definitions of the [ProtoBuf](https://developers.google.com/protocol-buffers/) data structure and the [GRPC](https://grpc.io/) interfaces can be found in the [tfserver-core](https://github.com/xiaoshenxian/tfserver-core) jar. Only this jar is needed for a client.


#### HTTP Json client

Supports only unary call for both GET and POST methods.

Request form:
- s=<sample_json>
- p=<param_json>

An example of <sample_json>:

```json
{
    "modelName":"the_model_name",
    "modelVersion":"01",
    "signatureName":"the_signature",
    "inputs":
    {
        "input1":
        {
            "data":[1.0, 2.0, 3.0, 4.0, 5.0, 6.0],
            "shape":[1, 2, 3]
        },
        "input2":
        {
            "data":["str1", "str2", "str3", "str4"],
            "shape":[1, 4]
        },
        "sequence_length":
        {
            "data":[4],
            "shape":[1]
        }
    }
}
```

An example of <param_json>:

```json
{
    "trace":"this_is_the_trace_str"
}
```

An example of a server return:

```json
{
    "status":
    {
        "code":0,
        "des":""
    },
    "data":
    {
        "the_output":
        {
            "type":"DT_FLOAT",
            "shape": [1, 16],
            "data":
            [
                [
                    -0.96606535,
                    -0.7829674,
                    -0.99842596,
                     0.77071506,
                     0.83995646,
                    -0.7173968,
                    -0.9995582,
                    -0.7859726,
                     0.41262636,
                     0.84797037,
                    -0.1419413,
                    -0.8871437,
                     0.8836554,
                     0.99823904,
                    -0.8551777,
                     0.96392214
                ]
            ]
        }
    }
}
```

##### Data structures

The Java *List* data converting tools and the "sample" request parameters can be found in the *ArrayWrapper4J* class and *Sample4J* class from the [tfserver-core](https://github.com/xiaoshenxian/tfserver-core). Blow gives a snapshot of the additional "*Param*" parameter and the "*Response*" data structure.

```java
// in Param.java

// Additional parameters
public class Param
{
    public String trace;// Using for tracing

    // ...
}

/**************** separator ****************/

// in Response.java

// The response:
public class Response
{
    public static class Status
    {
        public int code;// The status code
        public String des;// The status description

        // ...
    }

    public Status status;// The response status
    public Object data;// The results, in other word, the outputs of the model requested, is actually a map.

    // ...
}
```

### Server side

The GRPC request entrance is the *[TfServiceGrpc](./src/main/java/com/eroelf/tfserver/service/TfServiceGrpc.java)* class, while the HTTP Json entrance is the *[TfController](./src/main/java/com/eroelf/tfserver/controller/TfController.java)* class.

The GRPC interface is implemented according to the [official GRPC guide](https://grpc.io/docs/tutorials/basic/java.html), while the HTTP Json interface is implemented by using Tomcat.

The core data flow apart from the server framework is provided by the [tfserver-core](https://github.com/xiaoshenxian/tfserver-core). Please refer to the related classes for details.

#### About the "*config.properties*" file

This is the server configuration file for a specified Maven build profile. The configuration descriptions are shown below:
- `model_base_dir`: the base directory to place the TensorFlow exported models.
- `grpc_port`: the GRPC service port. Unlike the HTTP Json port which is specified in the Tomcat configuration file, the GRPC port should be specified here.
- `update_schedule_seconds`: In what frequency the server should refresh the "*model_base_dir*" to schedule an updating.

#### About the "*handler.properties*" file

This is the default model handler configuration file that is to be used to define a *ModelHandler*'s behavior. The configuration descriptions are shown below:
- `handler_class`: the specified class of the *ModelHandler*. This server uses the *LocalModelHandler* class which stores the Tensorflow model session in the local memory.
- `tags`: used by *SavedModelBundle* instances. See Tensorflow documents for detail. Defaulted by "serve" here.

Other parameters can be added for user specified *ModelHandler* implementations, e.g., distributed deployment.

A "conf" file can be added to both the model name paths and the model version paths to modify the default parameters or add other parameters.

## Authors

* **ZHONG Weikun**

## License

This project is released under the [Apache license 2.0](LICENSE).

```
Copyright 2018 ZHONG Weikun.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
