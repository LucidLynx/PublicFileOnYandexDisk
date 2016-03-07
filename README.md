## Описание

Maven-плагин нужен для публикации файла на Яндекс.Диске. Плагин выполняет один API-запрос в соответствии с [WebDAV API Диска] (https://tech.yandex.ru/disk/doc/dg/reference/publish-docpage/)

## Использование

```
<plugin>
    <groupId>ru.barefooter.maven.plugin</groupId>
    <artifactId>public-file-on-yandex-disk</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <goals>
                <goal>public-file</goal>
            </goals>
            <phase>package</phase>
            <configuration>
                <url>https://webdav.yandex.ru/example.jar</url>
                <serverId>my-yandex-disk</serverId>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Параметр serverId ссылается на сервер в настройках Maven (файл settings.xml).
