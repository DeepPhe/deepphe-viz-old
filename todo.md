* test
** configure travis with neo4j
** have it run the test

* basic display?
* routing is not working. why not?
*** test
* prototypes - something for phenotype level at top and  episodes below
* figure out what to do with list of bodysites
** who has bodysites?
** how to read and display them?
Done
====
* confirm that new repository functions
* get it to work with eclipse
* quick check to verify that changes in getPatients works
* see if we need some alternative data - add an ID to patient? does girish do that in the original code? hard to tell what Girish does.
* add id to dummy.
* change id to patient id in dummy data
* write getPatient in the data model utility
* verify why we're not seeing diagnoses and observations. ah. not handled.
* break get document into separate routine.
* test getDocumen
* test - can I get patientID out when I read it in...? - no patient id.
* add text to dummy data. tset 
* get document sub pieces 
		diagnosis
		medication
		Procedure
		add observation
* test
* push
*mail girish
*  test
** set up a test to populate dummy data and then get back out
** run as basic test

* basic display?
** getPatient 
***- first, what does query look like? http://localhost:7474/db/data/node/92/relationships/out/hasDiagnosis
*** write code in neo4jrest caller to create taht query
*** see http://neo4j.com/docs/stable/rest-api-cypher.html for notes.
*** write something like the get nodes with label, but build up query and do a post. 
*** finish get Patient
** getPatient 
*** finish makeCypherQuery