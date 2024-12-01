curl -v -u yourapikey:X \
  -H "Content-Type: application/json" \
  -X POST "https://domain.freshdesk.com/api/v2/custom_objects/schemas/30/records" \
  -d '{ "data": { "customer_id": "C1234", "customer_name": "Alan Turing", "age": 35 } }'