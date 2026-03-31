import { SetMetadata } from '@nestjs/common';

export const RAW_RESPONSE_METADATA_KEY = 'rawResponse';
export const RawResponse = () => SetMetadata(RAW_RESPONSE_METADATA_KEY, true);
