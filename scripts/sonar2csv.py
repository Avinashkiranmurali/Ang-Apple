__author__ = 'vprasanna'

import requests, json
import collections
from datetime import datetime
import pytz
import xlsxwriter

from office365.runtime.auth.authentication_context import AuthenticationContext
from office365.sharepoint.client_context import ClientContext


finalEndpoint = ''

def dictToQuery(d):
  query = ''
  for key in d.keys():
    query += str(key) + '=' + str(d[key]) + "&"
  return query

def quote(item, issue):
	if item['key'] in issue:
		s = issue[item['key']]
		if item['dataType'] == 'LIST':
			return str(', '.join(s))
		elif item['dataType'] == 'DATE':
			# 
			# print type(s)
			# print s
			parsed_date = datetime.strptime(s[:-6], item['pythonFormat'])
			return parsed_date
		else:
			return str(s)
	else:
		return ''

def parseResults(issueList, list):
	lines = []
	for issue in issueList['issues']: 
		row = dict()
		row['line'] = []
		row['issue'] = issue
		for item in list:
			row['line'].append(quote(item, issue))
		lines.append(row)
	return lines;

def findIssues(severities, types):
	# http://sonar2.cp.bridge2solutions.net:9000/api/issues/search?componentKeys=apple-gr%3Aapple-gr&s=FILE_LINE&resolved=false&types=VULNERABILITY&ps=100&facets=severities%2Ctypes&additionalFields=_all
	sonar2Endpoint = "http://sonar2.cp.bridge2solutions.net:9000/api/issues/search?"
	params = {
		'componentKeys':'apple-gr:apple-gr,com.b2s.rewards.service:discount-code-service,com.b2s.service.update.apple:apple-update-service',
		's':'FILE_LINE',
		# 'resolved':'false',
		'severities': severities,#'BLOCKER',
		'types': types,#'VULNERABILITY',
		'ps':'1000',
		'facets':'severities,types',
		'additionalFields':'_all'
	}
	qstr = dictToQuery(params)


	finalEndpoint = sonar2Endpoint + qstr;
	print "Final Endpoint:", finalEndpoint

	issues = requests.get(finalEndpoint)

	return json.loads(issues.text)


# http://sonar2.cp.bridge2solutions.net:9000/api/issues/search?componentKeys=apple-gr%3Aapple-gr&s=FILE_LINE&resolved=false&types=VULNERABILITY&ps=100&facets=severities%2Ctypes&additionalFields=_all


# print "issue -list", issueList['issues']
header_line = [{
		'label':'Key',
		'key':'key',
		'dataType':'STRING',
		'width': 25
	},{
		'label':'Project',
		'key':'project',
		'dataType':'STRING',
		'width': 40
	},{
		'label':'Component',
		'key':'component',
		'dataType':'STRING',
		'width': 105
	},{
		'label':'Rule',
		'key':'rule',
		'dataType':'STRING',
		'width': 10
	},{
		'label':'Severity',
		'key':'severity',
		'dataType':'STRING',
		'width': 10
	},{
		'label':'Line',
		'key':'line',
		'dataType':'STRING',
		'width': 5
	},{
		'label':'Status',
		'key':'status',
		'dataType':'STRING',
		'width': 7
	},{
		'label':'Message',
		'key':'message',
		'dataType':'STRING',
		'width': 70
	},{
		'label':'Type',
		'key':'type',
		'dataType':'STRING',
		'width': 12
	},{
		'label':'Author',
		'key':'author',
		'dataType':'STRING',
		'width': 30
	},{
		'label':'Tags',
		'key':'tags',
		'dataType':'LIST',
		'width': 35
	},{
		'label':'Creation Date',
		'key':'creationDate',
		'dataType':'DATE',
		'width': 20,
		'pythonFormat': '%Y-%m-%dT%H:%M:%S',
	},{
		'label':'Close Date',
		'key':'closeDate',
		'dataType':'DATE',
		'width': 20,
		'pythonFormat': '%Y-%m-%dT%H:%M:%S',
	},
]
 # = list #', '.join(list)


outputFile = 'Apple Sonar Scan Report.xlsx'

print 'Creating ', outputFile
workbook = xlsxwriter.Workbook(outputFile)
workbook.set_properties({
    'title':    'Apple GR Code Sonar Code Scan',
    'subject':  'Valnurablity Report',
    'author':   'Venkateswara Prasanna',
    'manager':  'Chris Watkins',
    'company':  'Bakkt',
    'category': 'Code Scan',
    'keywords': 'Java, Sonar',
    'created':  datetime.now(),
    'comments': 'Endpoint : ' + finalEndpoint})
# types = VULNERABILITY,BUG,CODE_SMELL
types= ['VULNERABILITY', 'BUG', 'CODE_SMELL']

# severities = BLOCKER,CRITICAL,MAJOR,MINOR,INFO
severities='BLOCKER,CRITICAL'

excelDateFormat = 'mmm d yyyy hh:mm AM/PM'

# For formatting options see: https://xlsxwriter.readthedocs.io/format.html
# Add a bold format to use to highlight cells.
headerFormat = workbook.add_format({'bold': True})
headerFormat.set_pattern(1)  # This is optional when using a solid fill.
headerFormat.set_bg_color('#9efff2')

defaultFormat = workbook.add_format({'bold': False})
defaultFormat_date = workbook.add_format({'bold': False, 'num_format': excelDateFormat})

closedFormat = workbook.add_format({'bold': False, 'font_strikeout': True})
closedFormat.set_pattern(1)  # This is optional when using a solid fill.
closedFormat.set_bg_color('#fcdcdc')

closedFormat_date = workbook.add_format({'bold': False, 'font_strikeout': True, 'num_format': excelDateFormat})
closedFormat_date.set_pattern(1)  # This is optional when using a solid fill.
closedFormat_date.set_bg_color('#fcdcdc')




for reportType in types:
	worksheet = workbook.add_worksheet(reportType)
	issueList = findIssues(severities=severities, types=reportType)
	lines = parseResults(issueList, header_line)
	# Start from the first cell. Rows and columns are zero indexed.
	row = 0
	col = 0

	#Record Counter
	worksheet.write(row, col, 'Sno', headerFormat)
	worksheet.set_column(col, col, 4)
	col+=1

	# Print Header
	for h in header_line:
		 worksheet.write(row, col, h['label'], headerFormat)
		 worksheet.set_column(col, col, h['width'])
		 col+=1

	row+=1
	# Iterate over the data and write it out row by row.
	for lineItem in lines:
		col=0
		#Record Counter
		worksheet.write(row, col, row)
		col+=1
		line = lineItem['line']
		closed = lineItem['issue']['status'] == 'CLOSED'
		for cellValue in line:
			# print 'date:' + str(header_line[col-2])
			isDate = header_line[col-1]['dataType'] == 'DATE'
			if closed:
				if isDate:
					# print 'date:' + str(cellValue)
					worksheet.write(row, col, cellValue, closedFormat_date)
				else:
					worksheet.write(row, col, cellValue, closedFormat)
			else:
				if isDate:
					# print 'date:' + str(cellValue)
					worksheet.write(row, col, cellValue, defaultFormat_date)
				else:
					worksheet.write(row, col, cellValue, defaultFormat)
			col+=1
		row += 1

workbook.close()