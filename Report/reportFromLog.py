#!/usr/bin/python
import urllib2
import json
import csv

logURL = "https://select.uni-trier.de/logger/fullLog.log"
report_filename = "report.csv"

dataLog = urllib2.urlopen(logURL).read().strip()

dataLogArray=[json.loads(line) for line in dataLog.split("\n")]

actions = {"verify":"receipts", "verified":"receipt", "go to BB":"receiptIdentifiers"}
report = {}
for el in dataLogArray:
    print el
    if el["action"] == "ballot cast":
        report[el["receiptID"]] = {x:False for x in actions.keys()} 
        report[el["receiptID"]]["email"] = el["email"]
    elif el["action"] in actions:
        keys = el[actions[el["action"]]]
        if keys == "":
            continue
        for key in keys.split(", "):
            if not key in report:
                print "No ballot cast for %s for %s" %(el["action"], key)
                continue 
            report[key][el["action"]] = True
    
with open(report_filename, 'w') as outfile:
    wr = csv.DictWriter(outfile, fieldnames = ['email'] + actions.keys())
    wr.writeheader()
    for receiptID, content in report.items(): 
        wr.writerow(content)
        
