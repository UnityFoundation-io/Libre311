import {z} from 'zod';

export const DeleteServiceRequestRequestSchema = z.object({
    service_request_id: z.number(),
    jurisdiction_id: z.string()
});

export type DeleteServiceRequestRequest = z.infer<typeof DeleteServiceRequestRequestSchema>;