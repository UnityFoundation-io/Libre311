import type { Mode } from './mode';
import { z } from 'zod';

type RecaptchaToken = string;
export interface RecaptchaService {
	execute(action: string): Promise<RecaptchaToken>;
}

export const RecaptchaServicePropsSchema = z.object({
	recaptchaKey: z.string()
});
export type RecaptchaServiceProps = z.infer<typeof RecaptchaServicePropsSchema>;

class RecaptchaServiceImpl implements RecaptchaService {
	private recaptchaKey: string;
	constructor(props: RecaptchaServiceProps) {
		this.recaptchaKey = props.recaptchaKey;
	}
	async execute(action: string): Promise<RecaptchaToken> {
		return await grecaptcha.enterprise.execute(this.recaptchaKey, { action });
	}
}

class MockRecaptchaService implements RecaptchaService {
	// eslint-disable-next-line @typescript-eslint/no-unused-vars
	async execute(action: string): Promise<string> {
		return '03AGdBq27tvcDrfi';
	}
}

export function recaptchaServiceFactory(
	mode: Mode,
	props: RecaptchaServiceProps
): RecaptchaService {
	return mode === 'test' ? new MockRecaptchaService() : new RecaptchaServiceImpl(props);
}
