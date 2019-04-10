# Distributed_Systems_Joke_Server
Returns randomized jokes and proverbs from server to each client independently

Homework Assignment: JokeServer
CSC 435 : Distributed Systems I
Professor Clark Elliott
Winter 2019

JokeServer
-	Connect from the admin client and automatically toggle the 
	server mode between joke and proverb mode on each connection
-	Return either a single joke or proverb depending on mode
-	Write server console output to log what has occurred
-	Return interleaved jokes and proverbs, depending on server
	mode at time of request, without losing track of cycles for
	each client
-	Parse username in JokeClient before entering request loop
-	Re-randomize jokes and proverbs for each client conversation
	at start of each cycle
-	Must be able to accomodate many clients without losing track
	of each client's joke/proverb cycle state
