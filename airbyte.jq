def p($p): "<p>\($p)</p>";

# input: array of arrays
def row2html:
  reduce .[] as $value ("<tr>"; . + "<td>\($value)</td>") + "</tr>";

# with style
def row2html($style):
  reduce .[] as $value ("<tr>";
     . + "<td style=\($style)><strong>\($value)</strong></td>") + "</tr>";

# input: an array of arrays, the first being treated as a header row
def table2html($tablestyle; $headerstyle):
  "<table style=\($tablestyle)>",
  "<tbody>",
   (.[0] | row2html($headerstyle)),
   (.[1:][] | row2html),
  "</tbody>",
  "</table>" ;

def atomicKeys2arrays:
  # emit an array of atomic keys
  def atomicKeys: to_entries | map( select(.value|scalars) | .key);
  (.[0] | atomicKeys) as $keys
  | $keys,
    (.[] | [ .[$keys[]]]);

def tableStyle: "\"border-collapse: collapse; width: 100%;\" border=\"1\"" ;
def headerStyle: "\"text-align: center;\"" ;

def table2html: table2html(tableStyle; headerStyle);

def airbyteParameters:
  .connectionSpecification.properties | .[] | [.title,(.type | ./"" | first |= ascii_upcase | add),.description] | join("|");

