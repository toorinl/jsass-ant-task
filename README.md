jsass-ant-task
=============

This is a simple SASS preprocessor Ant plugin. It makes use of [jsass][jsass] which uses [libsass][libsass] under the hood.

[libsass]: https://github.com/sass/libsass
[jsass]: https://github.com/bit3/jsass

Build
-----

Run `ant build make test` to build and test the jar. It will compile SASS files in the `test` directory to the `test-output` directory, see `build.xml` to see how it does that.

Example
------- 

```
<jsass todir="test-output" outputStyle="EXPANDED">
  <fileset dir="sass">
    <include name="**/*.scss"/>
  </fileset>
  <includePaths>
    <include name="sass-include"/>
  </includePaths>
  <mapper>
    <globmapper from="*.scss" to="*.expanded.css"/>
  </mapper>
</jsass>
```

The attribute [outputStyle](https://web-design-weekly.com/2014/06/15/different-sass-output-styles/) can be either `NESTED`, `EXPANDED`, `COMPACT` or `COMPRESSED`. The tag includePaths is an Ant DirSet.
