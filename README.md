# gitbucket-commitgraphs-plugin

This plugin enhances [takezoe/gitbucket](https://github.com/takezoe/gitbucket) by providing an viewing commit graphs.

## Features

It will viewing the graphs.

## Usage

- Open a shell window at the root of the project, hit `sbt package`
- if you update gitbucket-commitgraphs-plugin, remove any existing copy of gitbucket-commitgraphs-plugin from GITBUCKET_HOME/plugins
- Copy target/scala-2.11/gitbucket-commitgraphs-plugin-plugin_2.11-x.x.jar into GITBUCKET_HOME/plugins
- Restart GitBucket

## Release Notes

### 1.0

- introduce gitbucket-commitgraphs-plugin
