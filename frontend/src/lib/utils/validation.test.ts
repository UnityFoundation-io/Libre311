import { describe, it, expect } from 'vitest';
import { createUnvalidatedInput, optionalEmailValidator, emailValidator } from './validation';

describe('email validation test', () => {
	it('empty string is invalid', () => {
		const res = emailValidator(createUnvalidatedInput(''));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('');
	});
	it('improper format is invalid', () => {
		const res = emailValidator(createUnvalidatedInput('asdflk'));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('asdflk');
	});
	it('valid email passes validation', () => {
		const res = emailValidator(createUnvalidatedInput('test@example.com'));

		expect(res.value).toBe('test@example.com');
	});
});

describe('optional email validation test', () => {
	it('no input is valid', () => {
		const res = optionalEmailValidator(createUnvalidatedInput());
		expect(res.type).toBe('valid');
	});

	it('empty string is invalid', () => {
		const res = optionalEmailValidator(createUnvalidatedInput(''));

		expect(res.type).toBe('invalid');
	});
	it('improper format is invalid', () => {
		const res = optionalEmailValidator(createUnvalidatedInput('asdflk'));
		if (res.type != 'invalid') throw Error('Email Validator bug');
		expect(res.error).toBe('Invalid email');
		expect(res.value).toBe('asdflk');
	});
	it('valid email passes validation', () => {
		const res = optionalEmailValidator(createUnvalidatedInput('test@example.com'));
		console.log({ res });
		expect(res.value).toBe('test@example.com');
	});
});
