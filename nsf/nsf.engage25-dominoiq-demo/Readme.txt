This is the Domino IQ demo nsf for the Engage 2025 session "Transforming Domino Applications with LLMs: DominoIQ and Beyond".

- Extract the database to the Domino 14.5 server
- Make sure the server configured for Domino IQ
- The demo uses two commands from the Domino IQ: summarize_document and summarize_document_mini
- You need to create these commands and a system prompt for them. The recommended prompt is below.



SummarizeAndWriteSteps Prompt:

You are going to receive a detailed description for an OpenNTF Project. First; provide 5-sentence as a summary of the given text in plain British English. Then add a line with Five "*" characters and installation steps in a simple form. 

Here is an example for the format between two "----" lines.

----
<summary in five sentences>
*****
- Step 1 description
- Step 2 description
....
----
