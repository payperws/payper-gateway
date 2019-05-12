# Payper Gateway
[![Build Status](https://travis-ci.org/payperws/payper-gateway.svg?branch=master)](https://travis-ci.org/payperws/payper-gateway) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Payper Gateway is an HTTP (reverse) proxy that verifies payments to route requests. As a practical application, it can be used to sell API requests for digital cash (crypto-currency) on the Internet. See the detailed description in this [medium article](https://medium.com/@alexandru.males/payper-p2p-micro-payments-for-the-open-api-economy-e8adee76d396?fbclid=IwAR25i3YekBZ82ETVOTDaNnYH-zo7R4niaVHrhDRknwptWLlha8WwT-7ORp4).

## Concept
The gateway is part of a larger concept called Payper that deals with peer-to-peer (disintermediated) payment processing for APIs. Payper is a toolset that aims to bring together crypto-currency micropayments and APIs (web services in general) in order to allow open trading of digital resources on the Internet.

## Installation
Payper Gateway works as a backend java-based component, so it is no different than running an usual JVM based service. Until it is going to be available as a docker image or a fully executable jar in a release, it is necessary to compile and package in order to run it.
#### Prerequisites
* Java SDK 10 (or newer)
* Maven 3.5+
#### Dependencies
Payper Gateway uses [Hedera Java SDK](https://github.com/hashgraph/hedera-sdk-java) as a dependency that is not yet available in a maven repo. That is why it is necessary to install Hedera SDK separately.
```
git clone git@github.com:hashgraph/hedera-sdk-java.git
cd hedera-sdk-java
mvn install
```
#### Build and Run
1. Change dir into `payper-gateway` and build it:
```
mvn package
```

2. Configure your pricelist. As an example, refer to `application-price-list.yaml`. Create a file called exactly like that, `application-price-list.yaml` in your current directory.

3. Run the Payper Gateway jar from your current directory:
```
java -jar target/gateway-0.0.1-SNAPSHOT.jar
```
#### Test It
Open one of the configured URL in a browser. You will be prompted to make a payment in order to get access to the requested resource. The details on how to make the payment are listed in the Payment Required page.

## Implementation
This component basically integrates [Hedera Hashgraph](https://www.hedera.com) payment verification into an extension of [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway).

See [this article](https://medium.com/@alexandru.males/payper-p2p-micro-payments-for-the-open-api-economy-e8adee76d396?fbclid=IwAR25i3YekBZ82ETVOTDaNnYH-zo7R4niaVHrhDRknwptWLlha8WwT-7ORp4) for more information about how the project started and the plans for future.
