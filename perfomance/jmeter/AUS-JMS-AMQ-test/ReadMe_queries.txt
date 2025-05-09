Test plan to push multiple Message Queues to AUS using jMeter JMS Point-to-Point Sampler.

Prerequisites:
In order to restrict emails flooding to the user mailbox, use below query to Inactivate email notification
update var_program_notification set is_active = 0 where name = 'SHIPMENT_DELAY'

Use attached Test data(Test.csv) that are retrieved using below query:
select top 5000* from orders with (nolock) where var_id in ('Delta','UA','WF','RBC','Chase','FDR','FDR_PSCU','PNC','VitalityUS','SCOTIA') order by last_update desc

Verified DB update count using below queries:
select top 1000* from order_line with (nolock) where gateway_order_number != ''
select top 201* from status_change_queue with (nolock) order by process_date desc

Verified corresponding Application nodes processed logs using below Keywords:
AUS: Received update message (partnerOrderNumber=2100116813-APPLEGRQA2-785405736)
AppleGR: Loading order from database for OrderId: 2100116813

Switch between Local and DEV:
Local:
Provider URL    --> tcp://localhost:61616
JNDI properties --> queue.Q.REQ - AUS.local

DEV:
Provider URL    --> tcp://amq01.apexadev.bridge2solutions.net:61616
JNDI properties --> queue.Q.REQ - AUS.applegr.update.dev

Added below files in the same location for reference(Inference tab having VLOOKUP):
S-14320 Spike with 200 MQs.xlsx
S-14320 Spike with 4988 MQs.xlsx