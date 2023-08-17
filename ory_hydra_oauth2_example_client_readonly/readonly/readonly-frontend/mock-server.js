const jsonServer = require('json-server');
const server = jsonServer.create();
const router = jsonServer.router('mock-data.json');
const middlewares = jsonServer.defaults();

server.use(middlewares);

server.get('/logged-in', (req, res) => {
  res.status(401).jsonp({
    redirect_to: "https://ya.ru"
  })
})

server.use(router);

server.listen(3000, () => {
  console.log('JSON Server is running');
});