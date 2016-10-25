# exificient-for-javascript

EXI for JavaScript (Explorative) - How EXI can be used to represent JavaScript efficiently w.r.t.

* Size and
* Processing speed

[![Build Status](https://travis-ci.org/EXIficient/exificient-for-css.svg?branch=master)](https://travis-ci.org/EXIficient/exificient-for-javascript)

## Abstract Syntax Tree (AST) for JavaScript

We re-use the syntax tree format as standardized by [EStree project](https://github.com/estree/estree). See also
* [Esprima](http://esprima.org/)
* [SpiderMonkey/Parser_API](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/SpiderMonkey/Parser_API)

## Sample

### JavaScript

```javascript
var dog = 6, cat = 7, pig = dog * cat;
```

### EXI for JavaScript (visualized as XML)

```xml
<Program>
    <body>
        <array>
            <VariableDeclaration>
                <declarations>
                    <array>
                        <VariableDeclarator>
                            <id>
                                <Identifier>
                                    <name>dog</name>
                                </Identifier>
                            </id>
                            <init>
                                <Literal>
                                    <value>
                                        <integer>6</integer>
                                    </value>
                                </Literal>
                            </init>
                        </VariableDeclarator>
                    </array>
                </declarations>
                <kind>var</kind>
            </VariableDeclaration>
            <VariableDeclaration>
                <declarations>
                    <array>
                        <VariableDeclarator>
                            <id>
                                <Identifier>
                                    <name>cat</name>
                                </Identifier>
                            </id>
                            <init>
                                <Literal>
                                    <value>
                                        <integer>7</integer>
                                    </value>
                                </Literal>
                            </init>
                        </VariableDeclarator>
                    </array>
                </declarations>
                <kind>var</kind>
            </VariableDeclaration>
            <VariableDeclaration>
                <declarations>
                    <array>
                        <VariableDeclarator>
                            <id>
                                <Identifier>
                                    <name>pig</name>
                                </Identifier>
                            </id>
                            <init>
                                <BinaryExpression>
                                    <operator> </operator>
                                    <left>
                                        <Identifier>
                                            <name>dog</name>
                                        </Identifier>
                                    </left>
                                    <right>
                                        <Identifier>
                                            <name>cat</name>
                                        </Identifier>
                                    </right>
                                </BinaryExpression>
                            </init>
                        </VariableDeclarator>
                    </array>
                </declarations>
                <kind>var</kind>
            </VariableDeclaration>
        </array>
    </body>
</Program>
```

## Early Results

See test-data in https://github.com/EXIficient/exificient-for-javascript/tree/master/src/test/resources.

### Compression


| TestCase     | JavaScript [Size in Bytes]  | EXI 4 JS [Size in Bytes]  |
| ------------- |:-------------:| -----:|
| angular2.js     | 1182582 | 388994 |
| angular2.min.js | 635809 | 375410 |
|animals.js | 40 | 30 |
| browserDetection.js | 885 | 316 |
|  jquery.js | 263767 |72755 |
| jquery.min.js | 86355 | 58250 |
| react.js |  701412 | 200306 |
| react.min.js | 148805 | 101384 |
| xCryptic.app.js |5611 | 2508

On average "EXI for JS" is 50% of the size of the original JavaScript file!