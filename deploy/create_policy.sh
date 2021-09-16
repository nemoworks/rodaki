#!/bin/bash


curl -u ditto:ditto -X PUT -H 'Content-Type: application/json' -d '{
  "policyId": "ics.rodaki:base",
  "entries": {
    "owner": {
      "subjects": {
        "nginx:ditto": {
          "type": "nginx basic auth user"
        }
      },
      "resources": {
        "thing:/": {
          "grant": ["READ", "WRITE"],
          "revoke": []
        },
        "policy:/": {
          "grant": ["READ", "WRITE"],
          "revoke": []
        },
        "message:/": {
          "grant": ["READ", "WRITE"],
          "revoke": []
        }
      }
    }
  }
}' 'http://localhost:8080/api/2/policies/ics.rodaki:base'


curl -u ditto:ditto -X GET 'http://localhost:8080/api/2/policies/ics.rodaki:base'
