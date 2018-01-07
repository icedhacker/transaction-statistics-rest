# Statistics Aggregation

[![Build Status](https://travis-ci.org/icedhacker/transaction-statistics-rest.svg?branch=master)](https://travis-ci.org/icedhacker/transaction-statistics-rest)

A Sample Spring Boot project to calculate statistics for transactions done in the last 60 seconds

Create a restful API for statistics. The main use case for this API is to calculate realtime statistic from the last 60 seconds. There will be two APIs, one of them is called every time a transaction is made. It is also the sole input of this rest API. The other one returns the statistic based of the transactions of the last 60 seconds.
