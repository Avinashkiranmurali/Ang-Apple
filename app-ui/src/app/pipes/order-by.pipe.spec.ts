import { OrderByPipe } from '@app/pipes/order-by.pipe';

describe('OrderByPipe', () => {
  const orderByPipe = new OrderByPipe();
  let original: Array<number>;
  let sortedInAsec: Array<number>;
  let sortedInDec: Array<number>;

  beforeEach(() => {
    original = [2, 4, 1, 3];
    sortedInAsec = [1, 2, 3, 4];
    sortedInDec = [4, 3, 2, 1];
  });
  it('create an instance', () => {
    expect(orderByPipe).toBeTruthy();
  });

  it('should return the same value array is null', () => {
    expect(orderByPipe.transform(null, '', '')).toBe(null);
  });

  it('should return the same value array if its length is 1', () => {
    const data = ['test'];
    expect(orderByPipe.transform(data, 'asc')).toBe(data);
  });

  it('should return the same value if order is empty string', () => {
    expect(orderByPipe.transform([])).toEqual([]);
  });

  it('should sort the array if column is empty and order is asc', () => {
    expect(orderByPipe.transform(original, 'asc', '')).toEqual(sortedInAsec);
  });

  it('should sort the array in decending order if column is empty and order is not asc', () => {
    expect(orderByPipe.transform(original, 'dec', '')).toEqual(sortedInDec);
  });

  it('should sort the array in numeric order if column is empty and order is not in numeric', () => {
    expect(orderByPipe.transform(original, 'numericOrder', '')).toEqual(sortedInDec);
  });

  it('should sort a multi dimentional array based on the given column', () => {
    const users = [
      { user: 'fred',   age: 48 },
      { user: 'barney', age: 34 },
      { user: 'fred',   age: 40 },
      { user: 'barney', age: 36 }
    ];

    const orderedUsers = [
      { user: 'barney', age: 34 },
      { user: 'barney', age: 36 },
      { user: 'fred',   age: 48 },
      { user: 'fred',   age: 40 },
    ];

    expect(orderByPipe.transform(users, 'asc', 'user')).toEqual(orderedUsers);

  });
});
