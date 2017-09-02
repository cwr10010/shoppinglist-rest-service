import { InMemoryDbService } from 'angular-in-memory-web-api';

export class InMemoryDataService implements InMemoryDbService {
  createDb() {
    let shoppinglist = [
      { id: '1', name: 'Cheese', description: 'Nice Cheese' , order: 0, read: false },
      { id: '2', name: 'Milk', description: 'Sweet Milk' , order: 1, read: false },
      { id: '3', name: 'Butter', description: 'Salty Butter' , order: 2, read: false },
      { id: '4', name: 'Salad', description: 'Fresh Salad' , order: 3, read: false },
      { id: '5', name: 'Tomatoes', description: 'Red Tomatoes' , order: 4, read: false }
    ];
    const auth = { token: 'foo' }
    return {shoppinglist, auth};
  }
}
