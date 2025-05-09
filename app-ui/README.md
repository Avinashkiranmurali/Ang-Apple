# Apple GR Web Application - UI

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 9.1.5.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.
For windows users if the server did not start properly, please use the below commands,

```bash
$ npm run server-image
```
Open another tab/window and run the below command

```bash
$ npm run serve
```
## Proxy configuration for various environments

### Proxy to `https://localhost`

```bash
$ npm run proxy:local443
```

### Proxy to `https://localhost:8443`

```bash
$ npm run proxy:local8443
```

### Proxy to `https://ang-app-vip.apldev.bridge2solutions.net/`

```bash
$ npm run proxy:ang-dev
```

### Proxy to `https://ang-app-vip.aplqa1.bridge2solutions.net/`

```bash
$ npm run proxy:ang-qa1
```

### Proxy to `https://webapp-vip.aplqa2.bridge2solutions.net/`

```bash
$ npm run proxy:qa2
```

### Proxy to `https://webapp-vip.apluat.bridge2solutions.net/`

```bash
$ npm run proxy:uat
```

## Testing inside FRAME

To test the App inside FRAME, please open new bash/terminal window and navigate to `app-ui` folder and run the below
command.

```bash
$ npm run iframe
```

Now go to your browser and type `http://localhost:4000/`. This will load the application inside IFRAME

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also
use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag
for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
