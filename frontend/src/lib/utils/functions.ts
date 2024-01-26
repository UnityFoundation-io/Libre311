import L, { type PointTuple } from 'leaflet';
import { z } from 'zod';
import { isValidPhoneNumber, parsePhoneNumber } from 'libphonenumber-js';

export function sleep(ms: number) {
	return new Promise((resolve) => setTimeout(resolve, ms));
}

export function matchesDesktopMedia() {
	return window.matchMedia('(min-width: 64rem)').matches;
}

type AnchorPosition = 'center' | 'bottom-center';

function centerAnchor(iconSize: PointTuple): PointTuple {
	const [w, h] = iconSize;
	return [w / 2, h / 2];
}

function bottomCenterAnchor(iconSize: PointTuple): PointTuple {
	const [w, h] = iconSize;
	return [w / 2, h];
}

function createAnchor(iconSize: PointTuple, position: AnchorPosition): PointTuple {
	switch (position) {
		case 'center':
			return centerAnchor(iconSize);
		case 'bottom-center':
			return bottomCenterAnchor(iconSize);
	}
}

export function iconPositionOpts(
	aspectRatio: number,
	size: number,
	position: AnchorPosition = 'center'
): Pick<L.IconOptions, 'iconSize' | 'iconAnchor'> {
	const w = aspectRatio * size;
	const h = size;
	const iconSize: PointTuple = [w, h];

	return {
		iconSize,
		iconAnchor: createAnchor(iconSize, position)
	};
}

export function checkPhoneNumber(phoneNumber: string | undefined): string {
	if (phoneNumber == undefined) {
		return '';
	}

	try {
		isValidPhoneNumber(parsePhoneNumber(phoneNumber, { defaultCountry: 'US' }).number);

		return '';
	} catch (e: any) {
		let errorString = e.toString();

		errorString = errorString.replace(/ParseError\d+:\s/, '');
		errorString = errorString.replaceAll('_', ' ');
		errorString = errorString.toLowerCase();
		errorString = errorString.charAt(0).toUpperCase() + errorString.slice(1);

		return errorString;
	}
}

export type InputValidationState<T> = { valid: false; error: string } | { valid: true; value: T };

export type InputValidator<T> = (value: unknown) => InputValidationState<T>;

export function inputValidatorFactory<T>(schema: z.ZodType<T, z.ZodTypeDef, T>): InputValidator<T> {
    const validator: InputValidator<T> = (value: unknown) => {
        const res = schema.safeParse(value);
        if (res.success) return { valid: true, value: res.data };

        const firstIssue = res.error.issues.at(0);
        return { valid: false, error: firstIssue?.message ?? res.error.message };
    };

    return validator;
}

export function checkValid(validator: InputValidator<string>, value: string) {
	const isValid = validator(value);
	let error: string;

	if (!isValid.valid) {
		error = isValid.error;
	} else {
		error = '';
	}

	return error;
}

export const urlValidator: InputValidator<string> = inputValidatorFactory(z.string().url());
export const emailValidator: InputValidator<string> = inputValidatorFactory(z.string().email());
