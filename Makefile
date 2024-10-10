SPRING_PROFILE = prod

run:
	gradle bootRun --args='--spring.profiles.active=$(SPRING_PROFILE)'

docker-build:
	gradle  clean build jibDockerBuild

.PHONY: run, docker-build