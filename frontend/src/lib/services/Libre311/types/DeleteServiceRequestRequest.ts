import { z } from 'zod';

export const DeleteServiceRequestRequestSchema = z.object({
	service_request_id: z.number()
});

export type DeleteServiceRequestRequest = z.infer<typeof DeleteServiceRequestRequestSchema>;
