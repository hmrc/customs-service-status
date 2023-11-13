
# customs-service-status

This API is for updating service with status, and retrieve list of services status.


## Running the service locally
By default, the service runs on port 8991.

#### Using service manager 2

For running it locally use `sm2 --start CUSTOMS_SERVICE_STATUS`

the logs can be viewed by running `sm2 --logs CUSTOMS_SERVICE_STATUS` in a different terminal window

#### Using sbt
For local development use `sbt run` but if its already running in sm2, we should stop it first before running sbt command

## Customs Service Status API Endpoints

```PUT /services/{service name}/status```

e.g. PUT /services/haulier/status

```
If the service name has been configured, the response will be 200 OK 
Example request body string for state: "AVAILABLE" or "UNAVAILABLE"


If the service name has not been configured, the response will be 404 Not Found
Example error response : s"Service with name ${serviceName} not configured"

```

``` GET /services ```

Return a list of configured services and their status

e.g.

```
{
    "services": [
        {
            "name": "Haulier",
            "description": "GVMS Haulier API and UI",
            "state": "AVAILABLE",
            "lastUpdated": "2023-11-13T10:45:49.688Z"
        },
        {
            "name": "some_unknown_service",
            "description": "some service which is configured but no database status record",
            "state": "UNKNOWN" 
        }
    ]
}
```

If there are no service names configured, should return 200 with an empty list

```
{ "services": [] }
```


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").