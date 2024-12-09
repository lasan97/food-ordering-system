

## Dependency Graph
### [install graphviz](https://graphviz.org/download/)

#### mac - homebrew
```
brew install graphviz
```


### [depgraph-maven-plugin](https://github.com/ferstl/depgraph-maven-plugin)
```
mvn com.github.ferstl:depgraph-maven-plugin:aggregate -DcreateImage=true -DreduceEdges=false -Dscope=compile "-Dincludes=com.food.ordering.system*:*"
```