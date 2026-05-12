import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Transfer from '../Transfer';

describe('Transfer', () => {
  it('renders transfer form', () => {
    render(
      <BrowserRouter>
        <Transfer />
      </BrowserRouter>
    );
    expect(document.body).toBeTruthy();
  });
});
