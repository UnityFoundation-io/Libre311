import { describe, it, expect } from 'vitest';
import {
	createInput,
	optionalEmailValidator,
	emailValidator,
	optionalCoalesceNameValidator
} from './validation';

describe('emailValidator', () => {
	it('empty string is invalid', () => {
		const res = emailValidator(createInput(''));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('');
	});
	it('improper format is invalid', () => {
		const res = emailValidator(createInput('asdflk'));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('asdflk');
	});
	it('valid email passes validation', () => {
		const res = emailValidator(createInput('test@example.com'));

		expect(res.value).toBe('test@example.com');
	});
});

describe('optionalEmailValidator', () => {
	it('no input is valid', () => {
		const res = optionalEmailValidator(createInput());
		expect(res.type).toBe('valid');
		expect(res.value).toBe(undefined);
	});

	it('empty string is invalid', () => {
		const res = optionalEmailValidator(createInput(''));

		expect(res.type).toBe('invalid');
	});
	it('improper format is invalid', () => {
		const res = optionalEmailValidator(createInput('asdflk'));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('asdflk');
	});
	it('valid email passes validation', () => {
		const res = optionalEmailValidator(createInput('test@example.com'));
		expect(res.value).toBe('test@example.com');
	});
});

describe('optionalCoalesceNameValidator', () => {
	it('no input is valid', () => {
		const res = optionalCoalesceNameValidator(createInput());
		expect(res.type).toBe('valid');
		expect(res.value).toBe(undefined);
	});
	it('empty string is valid', () => {
		const res = optionalCoalesceNameValidator(createInput(''));
		expect(res.type).toBe('valid');
	});
	it('Numeric characters are invalid', () => {
		const res = optionalCoalesceNameValidator(createInput('4444'));
		expect(res.type).toBe('invalid');
		expect(res.error).toBe('Invalid');
	});
	it('valid name passes validation', () => {
		const res = optionalCoalesceNameValidator(createInput('Santiago Peña'));
		expect(res.type).toBe('valid');
		expect(res.value).toBe('Santiago Peña');
	});
});
