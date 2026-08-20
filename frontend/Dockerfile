FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json .
RUN npm ci
COPY . .
RUN npx ng build --configuration=production-static

FROM nginx:alpine
RUN apk add --no-cache gettext
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
ENV PORT=80
ENV API_URL=http://localhost:8080
CMD ["sh", "-c", "envsubst '$PORT' < /etc/nginx/conf.d/default.conf > /tmp/nginx.conf && mv /tmp/nginx.conf /etc/nginx/conf.d/default.conf && sed -i \"s|__API_URL__|$API_URL|g\" /usr/share/nginx/html/*.js && nginx -g 'daemon off;'"]
