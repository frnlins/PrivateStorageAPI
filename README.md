# PrivateStorageAPI

Este projeto é uma API REST para acesso e manipulação de um storage privado de objetos, nesse caso [minIO](https://min.io/download).

Para simular as chamadas foram utilizados o navegador (firefox, chrome, etc) e o [Postman](https://www.postman.com/).

Após subir a aplicação e o MinIO, o endereço http://localhost:8080/storage já vai fazer a primeira chamada listando todos os buckets.

### Tecnologias utilizadas
- Java 11
- Spring boot 2.4.4
- Minio SDK 8.2.0

### MinIO

Como fonte de consulta, [referência de API para Cliente Java](https://docs.min.io/docs/java-client-api-reference.html)

Foi utilizada a configuração básica, que se resume em:
- Fazer o download do executável do minio server: [minio.exe](https://min.io/download)
- Executar via terminal `minio.exe server .\{nome da pasta}`
- O MinIO já vem com uma interface que pode ser acessada através do http://localhost:9000
