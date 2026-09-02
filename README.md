# Kraken REST API Demo

A small Java example demonstrating authenticated requests to Kraken's Spot REST API.

The program:

- creates the Kraken `API-Sign` using SHA-256 and HMAC-SHA512;
- sends a validation-only `AddOrder` sell request;
- queries the private `Balance` endpoint;
- prints the request body, generated signature, HTTP status, and Kraken response.

## Safety

The `AddOrder` request uses:

```text
validate=true
```

so the example validates the order without executing a real trade. Do not change this to `false` unless you intentionally want to submit a live order.

Never commit Kraken API credentials to GitHub. This project reads them from environment variables.

## Requirements

- Java 11 or newer
- A Kraken API key and API secret with the permissions needed for the endpoints you want to test

## Set credentials

### macOS / Linux

```bash
export KRAKEN_API_KEY="your_api_key"
export KRAKEN_API_SECRET="your_api_secret"
```

### Windows PowerShell

```powershell
$env:KRAKEN_API_KEY="your_api_key"
$env:KRAKEN_API_SECRET="your_api_secret"
```

These commands set the credentials only for the current terminal session.

## Compile and run

From the repository directory:

```bash
javac src/Main.java
java -cp src Main
```

Example output may include:

```text
AddOrder Signature: ...
AddOrder Body: nonce=...&ordertype=limit&type=sell&volume=1.0&pair=XBTUSD&price=50000&validate=true
AddOrder Response: HTTP 200 - {"error":[], ...}
Balance Signature: ...
Balance Body: nonce=...
Balance Response: HTTP 200 - {"error":[], ...}
```

The exact Kraken response depends on the account, API permissions, and account status.

## Repository description

> Kraken Spot REST API authentication and trading examples in Java, including API signing, validation-only order submission, and balance queries.

## Credential warning

If an API key or secret was ever committed, uploaded, pasted publicly, or otherwise exposed, revoke it in Kraken and generate a new credential pair. Removing it from the latest source file does not make the old credential safe again.
