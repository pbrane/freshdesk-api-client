#First Create the ticket
curl -v -u "$FD_API_KEY":X -H "Content-Type: application/json" \
  -d '{ "email": "jimmy@beaconstrategists.com", "responder_id": 3043029172572, "subject": "OTDR Support Needed Again", "type": "Problem", "source": 3, "status": 2, "priority": 3, "description": "Another OTDR test ticket for me." }' \
  -X POST "$FD_BASE_URI/tickets" |jq > create_otdr-ticket.json


#Now create the TAC Case

##Straight from the API documentation
##This doesn't work and requests responder_id be in the request.
#curl -v -u "$FD_API_KEY":X \
#  -H "Content-Type: application/json" \
#  -d '{ "description": "Fail safe test ticket", "subject": "Test Ticket", "email": "david@beaconstrategists.com", "priority": 1, "status": 2, "cc_emails": ["jimmy@beaconstrategists.com","info@beaconstrategists.com"] }' \
#  -X POST "$FD_BASE_URI/tickets" |jq > create_example_ticket.json
