FROM node:22-alpine AS frontend-worker-build
WORKDIR /app
COPY . ./
RUN npm ci &&  npm run build
EXPOSE 5174
ENTRYPOINT ["npm", "run", "dev", "--", "--host", "0.0.0.0"]