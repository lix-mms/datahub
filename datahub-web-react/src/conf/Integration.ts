export enum IntegrationRoutes {
    // TODO How to configure EAM URL? Possible via Helm values? Via custom build process?
    // NOTE Configure your reverse proxy or load balancer to forward requests to proper upstream
    ENTITY_ACCESS_MANAGEMENT = '/eam',
}
