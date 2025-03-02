

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


## 빌드시
docker host 에러가 뜬다면 다음 명령어 실행
```bash
export DOCKER_HOST=unix:///Users/{본인유저명}/.docker/run/docker.sock
```

## 빌드 후 이미지 확인
```bash
docker images | grep food.ordering.system
```