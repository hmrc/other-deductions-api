Other Deductions API
========================
This API allows software packages to retrieve, create and amend, and delete deductions that have been previously
populated.

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager](https://github.com/hmrc/service-manager)

## Development Setup

Run from the console using: `sbt run` (starts on port 7797 by default)

Start the service manager profile: `sm2 --start MTDFB_OTHER_DEDUCTIONS`

## Running tests

```
sbt test
sbt it:test
```

## Viewing Open API Spec (OAS) docs

To view documentation locally ensure the Other Deductions API is running, and run api-documentation-frontend:
`./run_local_with_dependencies.sh`
Then go to http://localhost:9680/api-documentation/docs/openapi/preview and use this port and version:
`http://localhost:7797/api/conf/2.0/application.yaml`

## Changelog

You can see our changelog [here](https://github.com/hmrc/income-tax-mtd-changelog/wiki)

## Support and Reporting Issues

You can create a GitHub issue [here](https://github.com/hmrc/income-tax-mtd-changelog/issues)

## API Reference / Documentation

Available on
the [Other Deductions Documentation](https://developer.service.hmrc.gov.uk/api-documentation/docs/api/service/other-deductions-api)

## License

This code is open source software licensed under
the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html)
