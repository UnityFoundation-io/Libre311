compose_api:
	docker network create unity-network > /dev/null 2>&1 || true
	docker compose -f ./docker-compose.local.yml up libre311-db libre311-api

compose_ui:
	docker network create unity-network > /dev/null 2>&1 || true
	docker compose -f ./docker-compose.local.yml up libre311-db libre311-ui

compose_all:
	docker network create unity-network > /dev/null 2>&1 || true
	docker compose -f ./docker-compose.local.yml up